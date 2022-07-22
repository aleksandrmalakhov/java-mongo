package library;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    @BsonId
    private UUID id;

    @SerializedName("name")
    private String name;

    @BsonProperty("year")
    @SerializedName("year")
    private int yearOfWriting;
}