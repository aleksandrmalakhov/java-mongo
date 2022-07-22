import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.opencsv.exceptions.CsvException;
import junit.framework.TestCase;
import student.Course;
import student.Student;

import java.io.IOException;
import java.util.*;

public class MongoCSVTest extends TestCase {
    private MongoClient client;
    private MongoCreator mongoCreator;
    private static final String URL = "src/main/resources/mongo.csv";
    public static final String DATABASE_NAME = "library";
    private static final String COLLECTION_NAME = "students";


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

    public void testAddCSVtoCollection() throws IOException, CsvException {
        client.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME).drop();

        List<Student> list = mongoCreator.getStudentsList();
        long expected = list.size();

        mongoCreator.addAllDocumentsToCollection(list, Student.class, Course.class);
        long actual = client.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME).countDocuments();

        assertEquals(expected, actual);
    }

    public void testOldStudents() throws IOException, CsvException {
        List<Student> list = mongoCreator.getStudentsList();

        long expected = list.stream()
                .filter(student -> student.getAge() > 40)
                .toList()
                .size();

        long actual = mongoCreator.getCountStudentsOver40YearsOld();

        assertEquals(expected, actual);
    }

    public void testYoungestStudent() throws IOException, CsvException {
        Comparator<Student> studentComp = Comparator.comparing(Student::getAge).thenComparing(Student::getName);

        List<Student> list = mongoCreator.getStudentsList();
        list.sort(studentComp);

        String expected = list.get(0).getName();
        String actual = mongoCreator.getYoungestStudent().getName();

        assertEquals(expected, actual);
    }

    public void testCoursesOldestStudent() throws IOException, CsvException {
        Comparator<Student> studentComp = Comparator.comparing(Student::getAge).reversed().thenComparing(Student::getName);
        SortedSet<String> set = new TreeSet<>();
        StringJoiner listCourses = new StringJoiner("/");

        List<Student> list = mongoCreator.getStudentsList();
        list.sort(studentComp);

        Student student = list.get(0);
        student.getCourses().forEach(course -> set.add(course.getName()));
        set.forEach(listCourses::add);

        String expected = listCourses.toString();
        String actual = mongoCreator.getListCoursesOldestStudent();

        assertEquals(expected, actual);
    }

    @Override
    public void tearDown() {
        client.close();
    }
}
