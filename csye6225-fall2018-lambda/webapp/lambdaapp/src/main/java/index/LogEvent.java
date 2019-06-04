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
    private String DYNAMODB_TABLE_NAME = "csye6225";
    private Regions REGION = Regions.US_EAST_1;
    public  String FROM = "";
    static final String SUBJECT = "Password reset link";
    static String HTMLBODY;
    private static String TEXTBODY;
    static String token;
    static String username;

    public Object handleRequest(SNSEvent request, Context context) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

        String domainName = System.getenv("domainName");
        FROM = "noreply@test."+domainName;

        context.getLogger().log("Invocation started: " + timeStamp);

        long now = Instant.now().getEpochSecond(); // unix time
        long ttl = 60 * 1; // 24 hours in sec
        long totalttl= ttl+now;
        //Execution
        username = request.getRecords().get(0).getSNS().getMessage();
        context.getLogger().log( username );
        token = UUID.randomUUID().toString();
        context.getLogger().log("Invocation completed: " + timeStamp);
        try {
            context.getLogger().log("before created");
            initDynamoDbClient();
            context.getLogger().log("dynamo created");

            long ttlDbValue = 0;
                    Item item = this.dynamoDb.getTable(DYNAMODB_TABLE_NAME).getItem("id", username);
            if(item != null) {
                context.getLogger().log("Checking for timestamp");
                 ttlDbValue = item.getLong("ttl");
            }

            if (item == null || (ttlDbValue < now && ttlDbValue!=0)) {
                context.getLogger().log("Checking for timestamp and not null");


                    context.getLogger().log("user does not exist in the dynamo db table, create new token and send email");
                    this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                            .putItem(
                                    new PutItemSpec().withItem(new Item()
                                            .withString("id", username)
                                            .withString("token", token)
                                            .withLong("ttl", totalttl) ));


                    TEXTBODY = "https://"+domainName+"reset?email=" + username + "&token=" + token;
                    context.getLogger().log("Text " + TEXTBODY);
                    HTMLBODY = "<h2>Email sent from Amazon SES</h2>"
                            + "<p>Please reset the password using the below link. " +
                            "Link: https://csye6225-fall2018-shettypoo.me/reset?email=" + username + "&token=" + token + "</p>";
                    context.getLogger().log("This is HTML body: " + HTMLBODY);

                    //final String TEXTBODY = textBody;


                    AmazonSimpleEmailService clients = AmazonSimpleEmailServiceClientBuilder.standard()
                            .withRegion(Regions.US_EAST_1).build();
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
                    System.out.println("Email sent!");




        }
        else
        {
            context.getLogger().log("user exists in the dynamo db table");
        }
        } catch (Exception ex) {
            System.out.println( "The email was not sent. Error message: "
                    + ex.getMessage() );
        }

        return null;
    }

    private void initDynamoDbClient() {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();
        dynamoDb = new DynamoDB(client);


    }
}
