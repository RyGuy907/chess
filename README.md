# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```



phase 2 sequence diagram: https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUh2KohOhVk8iUKnU5XsKDAAFUOrCbndsYTFMo1Kp8UYdKUAGJITgwamURkwHRhOnAUaYHSQ4AAaz5HRgyQyqRgotGMGACClHDCKAAHtCNIziSyTqDcSpyvyoIycSIVKbCkdLjAFJqUMBtfUZegAKK6lTYAiJW3HXKnbLmcoAFicAGZuv1RupgOTxlMfVBvGV1W6PWF5NL0H0DugOJgjcz1A6inaUOU0D4EAgQ3jThWSapSiApZkrbSOoyGdpjVXTsZSgoOBw5QLtC37W3h5XO933ZkFD4wKlYcBN6lB+Wlx22ePJ9ON1ubWCF6dARdswjoci1I2sHfgdWndnrh07pNyqsTy7lu9QQIWaD-lM+whpQ1YRhg5QAExOE4CYDL+YpjDAAGPFMwGpKB4GQasJacKYXi+P4ATQOw5IwAAMhA0RJAEaQZFkyDmGyX7lNUdRNK0BjqAkaAJqqKCzC8bwcAcX5su+zo-kMmG7F8UlLKpALnB+Zq1uUCBMTysIGcxqLorE2LXoYi5Esu5T4dASAAF4oH24mSforxLEOtnHmOHIwAA4hSDGGUkACSaAAGbQJsQZoIevkmmGjrmnWM7WtoMDQDADZNhKUqysk3gOCqGFqjl6nvPO1m6SUlrBHujkuRwfoBvFMGwHBnEITAMYAIxoUmqgpvcfQZlm5Q+I1W7NSgyykWWVlsu2LLlKucgoBe274Qeq2joU44ulOLp7leaXVgp2aMaZL4IG+2mwSlNb1TASm3Cp2FQV8+GEUWX0kZ13U5GAvHIU4jRoeJxG4X0v1gf9OHFmYZGeN4fiBF4KDoKF9i+MwrHpJkmDwcwdXOhU0g+vRPr1D6zQtEJqgid08PgUDt6Pd+cN7n9EH-JgV3cWl+lMfjsI3fjZkYpZF02UyHZkhS207rzCNoD5CvJYdAUAGoakgWiZOqvLBcw4mJVro51eCp2XllOV5c2krurKPJUAb045Xq2BIJC06QOzy3PTxfIzakc2tf6KCBiJQPA5GfVOINvR9MNo0rBN0BTeHkfI6WNUrUea1GCg3DrnusLbXtxcHey5TSGXFKGNtMB84Xz1XaLeObpEqivoLXOfsUimySPILhj1oOVODzQJot5Ho1R-sMdCQWYayhPsSTU-C9QFOBbTDP2JhrNq0H4-yVzVw8yB6uaYPQJPalekwMgsSBRvqt3+Bsu1kXSVSQwHJGAFWbN0CaxHKyfy5R9bICNoYc4QUQoW32tAm2Fo7b7gdrAJ2BVXYwHdp7GAgd0Ad3JtmSkucoDOXmm1GOHU5IJ16gNIaLIM4TCztmaaDkaEtXzmRYOBJa6dmARSE+ox6LQirmdbQkDlwngCmbdeaopGxEtlA4er8JEoDUVgYOnMn7XWhJ-JMfcB5CxDuPK4UwdGqDTBUfoOjwrSDTP1RCsYoxPDYr2cqY0vg6AQKAaUVo-wTC+DogAcphcJewYCNDHvvCehRSZgxQpDXotiN4OKcZhFxbiPFeKmD41yfiVgBKCSAEJZTwlPCiTEvoxZ4kLzRpRQI2AfBQGwNweAPZDCmMMFvYmpM96vT4g0Y+p9w58wTPU0YiTQyFC7m9W+BF76fGmH0OZY1oKWJflQW2G1MgDNhHAPpAzpYWQ7sIwBoiQFgPPhAjRCiYEwDgYbTaJtpzKNQSIrRBzMHVxwblRsztCqELQB7eBJD1bkP2c6KhvDaFR3anHJhz1UlJxTomdhqZOGZmziqahyKBFlhdjKGAwwYjM1KtsuFL1nQACEPSXLAPQ2OwZ0WTxBtGFCbDkx4vGgS7MehpyQjRBiUl1yZAiPWuczCMj7byGmNs+RfkdblDOWuQwkSUDJBUWIAxSzr69O1Rcu6D0jH-MUlk0Y+TyjuM8TABZT1uWJwqLPBMtqUD2pgI6qMzqUZllaRjAIlgy4GX1QAKQgDyA1gRAnBJ3iDUZFMqiUgEi0HRZ8f7oATN04A4aoBwAgAZKAsxnHSBdSCY1Rib7gP5oBZ4QSi0lrLapTZlbxj+q0lajB6UABWsa0AnKHTyc1EqrlCJlbcpWoDK4NrVdreubzPafKQT8vxzy-L9vKEC+Q2VcGgvwRSoh0LSEJSNQyyhxKWrssYZfZh09WGp3ToKrhOckX8IXtOtBc6Tn7uACqvJ0gl11yOpSbACCDXbuSrug1gpHbHvJbKDUWp8zekvXLChe7cyekw-etFj6MVT15fGV9uKxofpzOhkhmGpW-tlSqKDm0TkDJrrcxRe6TpVBbbBSAbd3TDHpcsmN47MLmPuo-e81rvzVqfWkiGMB55BsXm0gIXhC3wG4HgdU2BumEHiIkFIRMOIppw5UKmNM6YM2MBzWt94ri9pk-BkAOm4R-wBdZG5VtRFufdB52D4GAqN3Li3fKaD-m237gonKhHgwoaMNoPQBgYCUCzIXWT5RIowmCAgLh8cFOVAAKzpLQtR2EHA1DdmIPFbKUU0vCsxAx7DDngTrXc5Jy1LmLM9HkyRnlM90nKd6AvIAA
