package chat.domain;

import chat.domain.*;
import chat.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class MessageSent extends AbstractEvent {

    private String messageId;
    private String roomId;
    private String userId;
    private String content;

    public MessageSent(Message aggregate) {
        super(aggregate);
    }

    public MessageSent() {
        super();
    }
}
//>>> DDD / Domain Event
