# Force Gradle Plugin


## Developer Info

Set the Salesforce username & password in a new file named `gradle.properties`:

    forceUsername=foo@bar.com
    forcePassword=passwordandauthtoken

Run the tests:

    ./gradlew -i test

Release:

    ./gradlew uploadArchives