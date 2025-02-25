package chat.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class EnterChatRoomCommand {

    private String userId;
    private String userName;
    private String userPassWord;
}
