Query existing credential store from code
=============================================================

This example demonstrates how to programmatically query credential store that was created with WildFly CLI.

Add a credential store with aliases via WildFly CLI:

```shell
/subsystem=elytron/credential-store=my_credential_store:add(location="PATH/TO/credential-store.cs", credential-reference={clear-text=StorePassword},create=true)
{"outcome" => "success"}
/subsystem=elytron/credential-store=my_credential_store:add-alias(alias=my-secret-db-password, secret-value="db-secret")
{"outcome" => "success"}
/subsystem=elytron/credential-store=my_credential_store:add-alias(alias=my-secret-access-password, secret-value="access-pw")
{"outcome" => "success"}
```
Credential Store has been created and 2 aliases `my-secret-access-password` and `my-secret-db-password` have been added.

Look at `ExistingCredentialStoreQueryExample` class to see how this credential store can be queried from code. For creation and population of credential store from code, please see `credential-store` example in this repository.

Run this command to execute this class:
```shell
    $ mvn clean install exec:exec
```

Expected output:
```
************************************
Current Aliases in credential store:
- my-secret-db-password
- my-secret-access-password
************************************
Your secret key for alias my-secret-db-password is: db-secret
```

**NOTE** this example uses clear text credentials. Do not use in production.