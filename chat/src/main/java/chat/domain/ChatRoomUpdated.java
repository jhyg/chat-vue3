package chat.domain;

import chat.domain.*;
import chat.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class ChatRoomUpdated extends AbstractEvent {

    private String roomId;
    private String roomName;

    public ChatRoomUpdated(ChatRoom aggregate) {
        super(aggregate);
    }

    public ChatRoomUpdated() {
        super();
    }
}
//>>> DDD / Domain Event
