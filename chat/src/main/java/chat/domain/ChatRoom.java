package chat.domain;

import chat.ChatApplication;
import chat.domain.ChatRoomDeleted;
import chat.domain.ChatRoomUpdated;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ChatRoom_table")
@Data
//<<< DDD / Aggregate Root
public class ChatRoom {

    @Id
    private String roomId;

    private String roomPw;

    private String roomName;

    @PostPersist
    public void onPostPersist() {
        ChatRoomDeleted chatRoomDeleted = new ChatRoomDeleted(this);
        chatRoomDeleted.publishAfterCommit();

        ChatRoomUpdated chatRoomUpdated = new ChatRoomUpdated(this);
        chatRoomUpdated.publishAfterCommit();
    }

    public static ChatRoomRepository repository() {
        ChatRoomRepository chatRoomRepository = ChatApplication.applicationContext.getBean(
            ChatRoomRepository.class
        );
        return chatRoomRepository;
    }

    //<<< Clean Arch / Port Method
    public void createChatRoom() {
        //implement business logic here:

        ChatRoomCreated chatRoomCreated = new ChatRoomCreated(this);
        chatRoomCreated.publishAfterCommit();
    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public void enterChatRoom(EnterChatRoomCommand enterChatRoomCommand) {
        //implement business logic here:

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
