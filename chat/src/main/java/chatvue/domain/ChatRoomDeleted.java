package chatvue.domain;

import chatvue.domain.*;
import chatvue.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class ChatRoomDeleted extends AbstractEvent {

    private String roomId;

    public ChatRoomDeleted(ChatRoom aggregate) {
        super(aggregate);
    }

    public ChatRoomDeleted() {
        super();
    }
}
//>>> DDD / Domain Event
