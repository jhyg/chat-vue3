package chatvue.domain;

import chatvue.domain.*;
import chatvue.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class ChatRoomCreated extends AbstractEvent {

    private String roomId;
    private String roomName;

    public ChatRoomCreated(ChatRoom aggregate) {
        super(aggregate);
    }

    public ChatRoomCreated() {
        super();
    }
}
//>>> DDD / Domain Event
