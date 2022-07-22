import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import library.Author;
import library.Book;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bson.Document;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.jsr310.Jsr310CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import student.Course;
import student.Student;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@AllArgsConstructor
@SuppressWarnings("unchecked")
public class MongoCreator {
    private MongoClient client;
    private String databaseName;
    private String collectionName;
    private String URL;
    private Gson gson;

    public void addAllDocumentsToCollection(List<?> list, Class<?>... dbClass) {
        try {
            client.getDatabase(databaseName)
                    .withCodecRegistry(CodecRegistries.fromProviders(PojoCodecProvider
                                    .builder()
                                    .register(dbClass)
                                    .register(Object.class)
                                    .build(),
                            new Jsr310CodecProvider(),
                            new ValueCodecProvider()))
                    .getCollection(collectionName, Object.class)
                    .insertMany(list);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public @NonNull List<Author> getListAuthors() {
        List<Author> listAuthors = new ArrayList<>();

        try {
            JSONObject obj = (JSONObject) new JSONParser().parse(new FileReader(URL));
            JSONArray array = (JSONArray) obj.get("authors");

            array.forEach(ob -> {
                Author author = gson.fromJson(ob.toString(), Author.class);
                author.setId(UUID.randomUUID());
                author.getListBooks().forEach(book -> book.setId(UUID.randomUUID()));

                listAuthors.add(author);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return listAuthors;
    }

    public String getListNameBook(String fieldName, String value) {
        SortedSet<String> sortSet = new TreeSet<>();
        StringJoiner actualStr = new StringJoiner("/");

        try {
            Objects.requireNonNull(client.getDatabase(databaseName)
                            .getCollection(collectionName)
                            .find().filter(new Document(fieldName, value)))
                    .cursor()
                    .forEachRemaining(author -> gson.fromJson(author.toJson(), Author.class)
                            .getListBooks()
                            .forEach(book -> sortSet.add(book.getName())));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        sortSet.forEach(actualStr::add);
        return actualStr.toString();
    }

    public String getOldBook() {
        Comparator<Book> bookComp = Comparator.comparing((Book::getYearOfWriting));
        StringJoiner result = new StringJoiner("/");

        String document = Objects.requireNonNull(client.getDatabase(databaseName)
                .getCollection(collectionName)
                .find()
                .sort(new Document("books.year", 1).append("name", 1))
                .first()).toJson();

        Author author = gson.fromJson(document, Author.class);

        assert author != null;
        List<Book> array = author.getListBooks();

        array.sort(bookComp);
        String nameBook = array.get(0).getName();
        String year = String.valueOf(array.get(0).getYearOfWriting());

        result.add(author.getName()).add(nameBook).add(year);
        return result.toString();
    }

    public Long getCountStudentsOver40YearsOld() {
        return client.getDatabase(databaseName)
                .getCollection(collectionName)
                .countDocuments(Document.parse("{age: {$gt: 40}}"));
    }

    public Student getYoungestStudent() {
        return getStudentSortByAgeAndName(1, 1);
    }

    public String getListCoursesOldestStudent() {
        SortedSet<String> set = new TreeSet<>();
        StringJoiner listCourses = new StringJoiner("/");
        Student student = getStudentSortByAgeAndName(-1, 1);

        student.getCourses().forEach(course -> set.add(course.getName()));
        set.forEach(listCourses::add);
        return listCourses.toString();
    }

    private Student getStudentSortByAgeAndName(int valueAge, int valueName) {
        String student = Objects.requireNonNull(client.getDatabase(databaseName)
                .getCollection(collectionName)
                .find()
                .sort(new Document("age", valueAge).append("name", valueName))
                .first()).toJson();

        return gson.fromJson(student, Student.class);
    }

    public List<Student> getStudentsList() throws IOException, CsvException {
        List<Student> studentList = new ArrayList<>();

        CSVReader reader = new CSVReader(new FileReader(URL));
        List<String[]> list = reader.readAll();

        list.forEach(line -> {
            if (line.length == 3) {
                List<Course> courseList = new ArrayList<>();

                String[] str = line[2].split(",");
                for (String name : str) {
                    Course course = new Course();
                    course.setId(UUID.randomUUID());
                    course.setName(name);

                    courseList.add(course);
                }

                Student student = new Student();
                student.setId(UUID.randomUUID());
                student.setName(line[0]);
                student.setAge(Integer.parseInt(line[1]));
                student.setCourses(courseList);

                studentList.add(student);
            }
        });
        return studentList;
    }
}