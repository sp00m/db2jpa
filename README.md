#Reverse Engineering from a Database to JPA Entities Based on the Remarks#

## DESCRIPTION ##

Reverse engineering strategy based on the "remarks" property of the database tables and fields.

The transformation rules applied to the remarks are:


    |   Remark   | Table to class | Field to property |
    |------------|----------------|-------------------|
    | the remark | TheRemark      | theRemark         |
    | the_remark | TheRemark      | theRemark         |
    | theremark  | Theremark      | theremark         |
    | theRemark  | Theremark      | theremark         |


If no remark has been set, the corresponding table/field real name will be used to proceed the transformation.

For example, given the below MySQL table named "tbl_user" with the comment "user":


    |         name         |     type     | ... |     comments      |
    |----------------------|--------------|-----|-------------------|
    | id_user              | int(11)      | ... |                   |
    | tx_email             | varchar(100) | ... | the email         |
    | tx_username          | varchar(20)  | ... | userName          |
    | tx_password          | char(40)     | ... | password          |
    | bl_need_confirmation | tinyint(1)   | ... | need_confirmation |


The following entity will be generated:

>    public class User {
>
>        private int idUser;
>        private String theEmail;
>        private String username;
>        private String password;
>        private boolean needConfirmation;
>
>        // ...
>
>    }


## QUICK START ##


 1. Configure the POM properties
 2. Run `mvn exec:exec`
 3. Check your destination dir
