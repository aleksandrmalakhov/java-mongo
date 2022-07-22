import com.google.gson.Gson;
import com.mongodb.MongoClient;
import junit.framework.TestCase;
import library.Author;
import library.Book;

import java.util.List;

public class MongoBooksTest extends TestCase {
    private MongoClient client;
    private MongoCreator mongoCreator;
    private static final String URL = "src/main/resources/authors.json";
    public static final String DATABASE_NAME = "library";
    private static final String COLLECTION_NAME = "books";
    public static final String AUTHOR_NAME = "М.А. Булгаков";
    public static final String EXPECTED_STR = "Записки юного врача/Мастер и Маргарита/Морфий/Собачье сердце";

    @Override
    public void setUp() {
        try {
            Gson gson = new Gson();
            client = new MongoClient();
            mongoCreator = new MongoCreator(client, DATABASE_NAME, COLLECTION_NAME, URL, gson);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void testAddDocumentsToCollection() {
        client.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME).drop();

        List<Author> list = mongoCreator.getListAuthors();
        long expected = list.size();

        mongoCreator.addAllDocumentsToCollection(list, Author.class, Book.class);
        long actual = client.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME).countDocuments();

        assertEquals(expected, actual);
    }

    public void testGetFavoriteAuthor() {
        String actualStr = mongoCreator.getListNameBook("name", AUTHOR_NAME);

        assertEquals(EXPECTED_STR, actualStr);
    }

    public void testOldBook() {
        String expected = "А.С. Пушкин/Капитанская дочка/1836";
        String actual = mongoCreator.getOldBook();

        assertEquals(expected, actual);
    }

    @Override
    public void tearDown() {
        client.close();
    }
}