package chat.infra;

import chat.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class ChatRoomHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<ChatRoom>> {

    @Override
    public EntityModel<ChatRoom> process(EntityModel<ChatRoom> model) {
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/createchatroom")
                .withRel("createchatroom")
        );
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/enterchatroom")
                .withRel("enterchatroom")
        );

        return model;
    }
}
