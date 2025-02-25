package chat.domain;

import chat.ChatApplication;
import chat.domain.MessageSent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Message_table")
@Data
//<<< DDD / Aggregate Root
public class Message {

    @Id
    private String messageId;

    private String roomId;

    private String userId;

    private String content;

    @PostPersist
    public void onPostPersist() {
        MessageSent messageSent = new MessageSent(this);
        messageSent.publishAfterCommit();
    }

    public static MessageRepository repository() {
        MessageRepository messageRepository = ChatApplication.applicationContext.getBean(
            MessageRepository.class
        );
        return messageRepository;
    }
}
//>>> DDD / Aggregate Root
