package index;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class LogEvent implements RequestHandler<SNSEvent, Object> {


    static DynamoDB dynamoDb;
   // private DynamoDB dynamoDb1;
    private String DYNAMODB_TABLE_NAME = "csye6225Db";
    private Regions REGION = Regions.US_EAST_1;
    static final String FROM = "noreply@csye6225-fall2018-shettypoo.me";
    static final String SUBJECT = "Password reset link";
    static String HTMLBODY;
    private static String TEXTBODY;
    static String token;
    static String username;

    public Object handleRequest(SNSEvent request, Context context) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
       // String accessKey = System.getenv("accessKey");
       // String secretKey = System.getenv("secretKey");
       // context.getLogger().log("access key sek"+accessKey.contains("AKI")+secretKey.contains(""));
        //Loggers
        context.getLogger().log("Invocation started: " + timeStamp);
        context.getLogger().log("1: " + (request == null));
        context.getLogger().log("2: " + (request.getRecords().size()));
        context.getLogger().log(request.getRecords().get(0).getSNS().getMessage());
        timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        context.getLogger().log("Invocation completed: " + timeStamp);

        context.getLogger().log("step 1");
        long now = Instant.now().getEpochSecond(); // unix time
        long ttl = 60 * 20; // 24 hours in sec

        //Execution
        username = request.getRecords().get(0).getSNS().getMessage();
        context.getLogger().log( username );
        token = UUID.randomUUID().toString();

        try {
            context.getLogger().log("before created");
            initDynamoDbClient(context);
            context.getLogger().log("dynamo created");
//         AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
//                .withRegion(Regions.US_EAST_1)
//                .build();
//        context.getLogger().log( "asda" );
//         dynamoDb = new DynamoDB(client);//GetItemSpec
            Item item = this.dynamoDb.getTable(DYNAMODB_TABLE_NAME).getItem("id", username);

//                Calendar cal = Calendar.getInstance();
//                Date d = new Date();
//                cal.add(Calendar.MINUTE, 20);
//            Date dten = cal.getTime();
//            String dtwenty=new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(dten);
//
            if (item == null) {

                context.getLogger().log("item is not null");
                //if(cal)
                //String newTime = df.format(cal.getTime());
                  //context.getLogger().log("dynamo get data present: "+ dateDB);
            //    if (true) {
                    context.getLogger().log("user does not in the dynamo db table, create new token and send email");
                    this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                            .putItem(
                                    new PutItemSpec().withItem(new Item()
                                            .withString("id", username)
                                            .withString("token", token)
                                            .withLong("ttl", ttl+now ) ));


                    TEXTBODY = "https://csye6225-fall2018-shettypoo.me/reset?email=" + username + "&token=" + token;
                    context.getLogger().log("This is text body: " + TEXTBODY);
                    HTMLBODY = "<h2>You have successfully sent an Email using Amazon SES!</h2>"
                            + "<p>Please reset the password using the below link. " +
                            "Link: https://csye6225-fall2017.com/reset?email=" + username + "&token=" + token + "</p>";
                    context.getLogger().log("This is HTML body: " + HTMLBODY);

                    //final String TEXTBODY = textBody;

                    context.getLogger().log("step 2");

                    AmazonSimpleEmailService clients = AmazonSimpleEmailServiceClientBuilder.standard()
                            .withRegion(Regions.US_EAST_1).build();
                    context.getLogger().log("step 3");
                    SendEmailRequest emailRequest = new SendEmailRequest()
                            .withDestination(
                                    new Destination().withToAddresses(username))
                            .withMessage(new Message()
                                    .withBody(new Body()
                                            .withHtml(new Content()
                                                    .withCharset("UTF-8").withData(HTMLBODY))
                                            .withText(new Content()
                                                    .withCharset("UTF-8").withData(TEXTBODY)))
                                    .withSubject(new Content()
                                            .withCharset("UTF-8").withData(SUBJECT)))
                            .withSource(FROM);
                    clients.sendEmail(emailRequest);
                    context.getLogger().log("step 4");
                    System.out.println("Email sent!");



//            } else {
//                context.getLogger().log("TTL is expired");
//            }
        }
        else
        {
            context.getLogger().log("user exists in the dynamo db table");
        }
        } catch (Exception ex) {
            System.out.println( "The email was not sent. Error message: "
                    + ex.getMessage() );
            context.getLogger().log( "step 5" );
        }

        return null;
    }

    private void initDynamoDbClient(Context context) throws Exception{
     //   String accessKey = System.getenv("accessKey");
      //  String secretKey = System.getenv("secretKey");
       // context.getLogger().log("access key sek"+accessKey.contains("AKI")+secretKey.contains(""));
        try {
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.US_EAST_1)
                    .build();
            context.getLogger().log( "asda" );
            dynamoDb = new DynamoDB(client);
        }
        catch(Exception e){
            context.getLogger().log( "asda"+e.getMessage() );
        }
//        AWSCredentials awsCreds = new EnvironmentVariableCredentialsProvider().getCredentials();
//        context.getLogger().log("asd");
//        AmazonDynamoDBClient client = new AmazonDynamoDBClient(awsCreds).withRegion(Regions.US_EAST_1);
//        context.getLogger().log("client env"+client);
//        System.out.println("got the dbclient for useast1");
//         this.dynamoDb = new DynamoDB(client);
//
//        AmazonDynamoDBClient client1 = new AmazonDynamoDBClient();
//        client1.setRegion(Region.getRegion(REGION));
//        this.dynamoDb1 = new DynamoDB(client1);
//        context.getLogger().log("client1 . to string"+client1.toString());
//        AmazonDynamoDBClient client =  new AmazonDynamoDBClient(new BasicAWSCredentials(accessKey,secretKey));
//        context.getLogger().log("client . to string"+client.toString());
//        client.setRegion(Region.getRegion(REGION));
//        this.dynamoDb = new DynamoDB(client);
//        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
//
//            credentialsProvider.getCredentials();

//        DynamoDB dynamoDb = AmazonDynamoDBClientBuilder.standard()
//                .withCredentials(credentialsProvider)
//                .withRegion("us-east-1")
//                .build();

    }
}
