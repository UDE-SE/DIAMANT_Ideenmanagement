package de.unidue.se.diamant.backend.backendservice.service.chat;

import de.unidue.se.diamant.backend.backendservice.service.chat.domain.ChatEntry;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private MongoTemplate mongoTemplate;

    @Autowired
    public ChatService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void saveTeamMessage(String ideaId, ChatEntry newEntry) {
        Query query1 = new Query(Criteria.where("id").is(ideaId));
        Update update1 = new Update();
        update1.addToSet("teamChat", newEntry);
        mongoTemplate.updateFirst(query1, update1, Idea.class);
    }

    public void saveRefereesMessage(String ideaId, ChatEntry newEntry) {
        Query query1 = new Query(Criteria.where("id").is(ideaId));
        Update update1 = new Update();
        update1.addToSet("refereesChat", newEntry);
        mongoTemplate.updateFirst(query1, update1, Idea.class);
    }

    public void saveTeamAndRefereesMessage(String ideaId, ChatEntry newEntry) {
        Query query1 = new Query(Criteria.where("id").is(ideaId));
        Update update1 = new Update();
        update1.addToSet("teamAndRefereesChat", newEntry);
        mongoTemplate.updateFirst(query1, update1, Idea.class);
    }
}
