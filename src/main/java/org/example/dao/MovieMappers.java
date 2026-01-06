package org.example.dao;

import org.bson.Document;
import org.example.model.Movie;
import org.example.model.Personnel;

import java.util.List;
import java.util.Objects;

/**
 * Helper methods for mapping Mongo data into domain models and vice versa.
 */
public class MovieMappers {
    public static final String NAME = "name";
    public static final String YEAR = "year";
    public static final String DIRECTOR = "director";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String AGE_LIMIT = "ageLimit";
    public static final String RATING = "rating";
    public static final String SYNOPSIS = "synopsis";
    public static final String ACTORS = "actors";

    private MovieMappers() {
        // Not meant to be instantiated
    }

    public static Movie fromDocument(Document document) {
        final Movie movie = new Movie();

        movie.setName(document.getString(NAME));
        movie.setYear(document.getInteger(YEAR));
        movie.setAgeLimit(document.getInteger(AGE_LIMIT));
        movie.setRating(document.getInteger(RATING));
        movie.setSynopsis(document.getString(SYNOPSIS));

        if (document.get(DIRECTOR, Document.class) != null) {
            final Personnel director = fromSubDocument(document.get(DIRECTOR, Document.class));
            movie.setDirector(director);
        }

        List<Document> actorDocs = document.getList(ACTORS, Document.class);
        if (actorDocs != null) {
            List<Personnel> actors =
                    actorDocs.stream().filter(Objects::nonNull).map(MovieMappers::fromSubDocument)
                            .toList();
            movie.setActors(actors);
        }

        return movie;
    }

    public static Document toDocument(Movie movie) {
        Document doc = new Document();

        doc.append(NAME, movie.getName());
        doc.append(YEAR, movie.getYear());
        doc.append(AGE_LIMIT, movie.getAgeLimit());
        doc.append(RATING, movie.getRating());
        doc.append(SYNOPSIS, movie.getSynopsis());

        if (movie.getDirector() != null) {
            Document directorDoc =
                    new Document().append(FIRST_NAME, movie.getDirector().getFirstName())
                            .append(LAST_NAME, movie.getDirector().getLastName());
            doc.append(DIRECTOR, directorDoc);
        }

        if (movie.getActors() != null && !movie.getActors().isEmpty()) {
            List<Document> actorDocs = movie.getActors().stream()
                    .map(a -> new Document().append(FIRST_NAME, a.getFirstName())
                            .append(LAST_NAME, a.getLastName())).toList();
            doc.append(ACTORS, actorDocs);
        }

        return doc;
    }

    private static Personnel fromSubDocument(Document doc) {
        Personnel p = new Personnel();
        p.setFirstName(doc.getString(FIRST_NAME));
        p.setLastName(doc.getString(LAST_NAME));
        return p;
    }
}
