package chat.infra;

import chat.domain.*;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//<<< Clean Arch / Inbound Adaptor

@RestController
// @RequestMapping(value="/chatRooms")
@Transactional
public class ChatRoomController {

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @RequestMapping(
        value = "/chatRooms/{id}/createchatroom",
        method = RequestMethod.PUT,
        produces = "application/json;charset=UTF-8"
    )
    public ChatRoom createChatRoom(
        @PathVariable(value = "id") String id,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws Exception {
        System.out.println("##### /chatRoom/createChatRoom  called #####");
        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findById(id);

        optionalChatRoom.orElseThrow(() -> new Exception("No Entity Found"));
        ChatRoom chatRoom = optionalChatRoom.get();
        chatRoom.createChatRoom();

        chatRoomRepository.save(chatRoom);
        return chatRoom;
    }

    @RequestMapping(
        value = "/chatRooms/{id}/enterchatroom",
        method = RequestMethod.PUT,
        produces = "application/json;charset=UTF-8"
    )
    public ChatRoom enterChatRoom(
        @PathVariable(value = "id") String id,
        @RequestBody EnterChatRoomCommand enterChatRoomCommand,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws Exception {
        System.out.println("##### /chatRoom/enterChatRoom  called #####");
        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findById(id);

        optionalChatRoom.orElseThrow(() -> new Exception("No Entity Found"));
        ChatRoom chatRoom = optionalChatRoom.get();
        chatRoom.enterChatRoom(enterChatRoomCommand);

        chatRoomRepository.save(chatRoom);
        return chatRoom;
    }
}
//>>> Clean Arch / Inbound Adaptor
