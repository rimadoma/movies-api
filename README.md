This App offers a REST API into movie data that's hosted in a Mongo Atlas cluster. The API is defined and generated with OpenAPI. See the definition `src\main\java\resources\movies.yml` for details on the endpoints. The app uses the Spring framework for configuration and creating an app around the generated API code.

# Huomioita tehtävän annosta
Mietin, että hakutermillä haku voisi hakea useampaa kenttää, kuten elokuvan nimeä, näyttelijöitä ja synoposista, mutta toisaalta nimihaku, näyttelijähaku yms. erikseen tuntuvat
intuitiivisemmalta. Toteutin sumean haun elokuvan nimellä, koska oletin että APIa kutsutaan enemmän tai vähemmän suoraan frontendistä / loppukäyttäjiltä. Tähän tarkoitukseen Mongo Atlaksen tarjoama Search Index soveltuu hyvin. Jos APIa käyttäisivät toiset ohjelmistokomponentit, kannattaisi käyttää perinteisiä indeksejä, koska ne ovat tehokkaampia ja vievät vähemmän resursseja.

Jatkokehityksen kohteita on merkkailtu TO DO -kommenteilla oleellisiin kohtiin koodissa.

# Search by partial movie name
The endpoint `movies/{partialName}` returns all movies whose name has a word that fuzzily matches the given partial name. This helps you find movies even if you make a (single) typo. For example, searching for "pro" (case-insensitive) returns "The Florida Project", "Predator", and "Saving Private Ryan", but it would not return "Improper Conduct". The endpoint utilises a special search index that's only available in Mongo Atlas. Great for user interaction, but for exact matches etc. you're better off a traditional index. You can make your search index support both. The current search index may need further tweaks for optimal read / write performance, size on disk etc.

# Prerequisites
You're probably OK with the JDK and Maven versions that come bundled in your IDE, but in case there are any compatibility issues:

1. Java 25, and JAVA_HOME pointing to that version
2. Maven 3.x.x
3. Ensure with mvn --version that it uses the same JDK as in JAVA_HOME
4. Ask Richard for the Mongo Atlas password (goes into `src\main\java\resources\application.properties`)
   * If you have any access problems, ask Richard to check cluster config
5. Test adding movies with `curl -X POST "http://localhost:8080/movies" -H "Content-Type: application/json"   -d '{"name": "Naked Gun","year": 1982}'`


# Local development
1. Build with `mvn clean install` at the root of the repo
2. Add a run time configuration for App in your IDE (if needed)
3. Run the App class, the API launches at http://localhost:8080 by default
    * You can change the port in application.properties
4. Test the endpoints with your favourite browser / API tool, e.g. http://localhost8080/movies/

## Troubleshooting
If your IDE is giving build trouble over the generated code, add the POM inside generated-sources\openapi as a Maven project (i.e. in IntelliJ right-click the POM -> Add as Maven project).