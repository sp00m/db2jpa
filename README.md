#README
Reverse Engineering from a Database to JPA Entities Based on the Remarks

## DESCRIPTION

Reverse engineering strategy based on the `remarks` property of the database tables and fields.

The transformation rules applied to the remarks are:

<table>
    <tr>
        <th>Remark</th><th>Table to class</th><th>Field to property</th>
    </tr>
    <tr>
        <td>the remark</td><td>TheRemark</td><td>theRemark</td>
    </tr>
    <tr>
        <td>the_remark</td><td>TheRemark</td><td>theRemark</td>
    </tr>
    <tr>
        <td>theremark</td><td>Theremark</td><td>theremark</td>
    </tr>
    <tr>
        <td>theRemark</td><td>Theremark</td><td>theremark</td>
    </tr>
</table>

If no remark has been set, the corresponding table/field real name will be used to proceed the transformation.

For example, given the below MySQL table named `tbl_user` with the comment `user`:

<table>
    <tr>
        <th>name</th><th>type</th><th>...</th><th>comments</th>
    </tr>
    <tr>
        <td>id_user</td><td>int(11)</td><td>...</td><td></td>
    </tr>
    <tr>
        <td>tx_email</td><td>varchar(100)</td><td>...</td><td>the email</td>
    </tr>
    <tr>
        <td>tx_username</td><td>varchar(20)</td><td>...</td><td>userName</td>
    </tr>
    <tr>
        <td>tx_password</td><td>char(40)</td><td>...</td><td>password</td>
    </tr>
    <tr>
        <td>bl_need_confirmation</td><td>tinyint(1)</td><td>...</td><td>need_confirmation</td>
    </tr>
</table>

The following entity will be generated:
```java
public class User {

    private int idUser;
    private String theEmail;
    private String username;
    private String password;
    private boolean needConfirmation;

    // ...

}
```

## QUICK START


 1. Configure the POM properties
 2. Run `mvn exec:exec`
 3. Check your destination dir