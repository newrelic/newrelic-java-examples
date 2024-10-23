# mongodb-sync-driver

Example app using the Mongodb sync driver with New Relic instrumentation.

Install the community edition of Mongodb using the steps here:
https://www.mongodb.com/docs/manual/tutorial/install-mongodb-on-os-x/

The New Relic agent jar will need to be attached to the application via the `-javaagent` JVM arg. For example:
`-javaagent:/path/to/newrelic.jar`

Make sure you have a valid yaml config file (or associated system properties and/or environment variables) to 
properly configure the agent.
