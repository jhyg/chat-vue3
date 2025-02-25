package chat.domain;

import chat.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "chatRooms", path = "chatRooms")
public interface ChatRoomRepository
    extends PagingAndSortingRepository<ChatRoom, String> {}
