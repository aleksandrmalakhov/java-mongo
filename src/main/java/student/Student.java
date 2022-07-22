package student;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @BsonId
    private UUID id;

    @SerializedName("name")
    private String name;

    @SerializedName("age")
    private int age;

    @SerializedName("courses")
    private List<Course> courses;
}