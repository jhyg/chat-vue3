package chatvue.domain;

import chatvue.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "messages", path = "messages")
public interface MessageRepository
    extends PagingAndSortingRepository<Message, String> {}
