# csye6225-fall2018-repo-template

When the user wants to reset the password, the user/password API will be called. This request will be sent to LambdaApp using the SNS Topic. The Lambda App will process the user request and send an email notification to the user on the registered email address.
/public address:8080/CloudApp/user/reset

Commands:
1. network: 
bash csye6225-aws-cf-create-stack.sh networkstack
2.cicd: 
bash csye6225-aws-cf-create-cicd-stack.sh cicdstack code-deploy.csye6225-fall2018-shettypoo.me
3. auto-scaling: 
bash csye6225-aws-cf-create-auto-scaling-application-stack.sh appstack csye6225keys imagebkt94 rootusr root-password codedeploy.lambda.shettypoo.me password_reset csye6225-fall2018-shettypoo.me csye6225-cloudapp

serverless --> 
bash csye6225-aws-cf-create-serverless-stack.sh serverlessstack password_reset csye6225-fall2018-shettypoo.me

RUN TRAVIS

4. bash csye6225-aws-cf-create-waf-stack.sh securitystack


Delete Stack:
1.
bash csye6225-aws-cf-terminate-waf-stack.sh securitystack

serverless:
bash csye6225-aws-cf-terminate-serverless-stack.sh serverlessstack


2. bash csye6225-aws-cf-terminate-auto-scaling-application-stack.sh appstack csye6225keys imagebkt94 rootusr root-password codedeploy.lambda.shettypoo.me password_reset csye6225-fall2018-shettypoo.me csye6225-cloudapp

3) bash csye6225-aws-cf-terminate-cicd-stack.sh cicdstack code-deploy.csye6225-fall2018-shettypoo.me csye6225-cloudapp

4)bash csye6225-aws-cf-terminate-stack.sh networkstack

ssh in: ssh -i csye6225.pem centos@35.175.124.94
   a. sudo -s
   b. yum install mysql mysql-server mysql-devel
   c. mysql -h csye6225-fall2018.c8y14gdovhd9.us-east-1.rds.amazonaws.com -u rootusr -P 3306 -p
   d. use csye6225
   e.select * from userdata;
       select * from transaction_data;
       select * from attachment;

Installing Hydra:
git clone https://github.com/vanhauser-thc/thc-hydra.git
cd thc-hydra
./configure
make
sudo make install
create password.txt file in /home/poojithsshetty so link is /home/poojithsshetty/password.txt

hydra -l poojithsshetty@gmail.com -P /home/poojithsshetty/password.txt -vV csye6225-fall2018-shetty.poo.me http-get "/CloudApp/time" -s 443 -S -f


ssh in: 
ssh -i csye6225.pem centos@35.175.124.94
   a. sudo -s
   b. yum install mysql mysql-server mysql-devel
   c. mysql -h csye6225-fall2018.c8y14gdovhd9.us-east-1.rds.amazonaws.com -u rootusr -P 3306 -p
   d. use csye6225
   e.select * from userdata;
       select * from transaction_data;
       select * from attachment;
