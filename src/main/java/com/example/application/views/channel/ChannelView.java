package com.example.application.views.channel;

import com.example.application.chat.ChatService;
import com.example.application.chat.Message;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.List;

@Route(value = "channel")
public class ChannelView extends VerticalLayout
        implements HasUrlParameter<String> {

    private final ChatService chatService;
    private final MessageList messageList;
    private final List<Message> receivedMessages = new ArrayList<>();

    private String channelId;

    public ChannelView(ChatService chatService) {
        this.chatService = chatService;
        setSizeFull();
        messageList = new MessageList();
        messageList.setSizeFull();
        add(messageList);
        var messageInput = new MessageInput(event->sendMessage(event.getValue()));
    }

    @Override
    public void setParameter(BeforeEvent event, String channelId) {
        if (chatService.channel(channelId).isEmpty()) {
            throw new IllegalArgumentException("Invalid channel ID");
        }
        this.channelId = channelId;
    }

    private void sendMessage(String message) {
        if (!message.isBlank()) {
            chatService.postMessage(channelId, message);
        }
    }

    private MessageListItem createMessageListItem(Message message) {
        var item = new MessageListItem(
                message.message(),
                message.timestamp(),
                message.author()
        );
        return item;
    }

    private void receiveMessages(List<Message> incoming) {


        getUI().ifPresent(ui -> ui.access(() -> {


            receivedMessages.addAll(incoming);
            messageList.setItems(receivedMessages.stream()
                    .map(this::createMessageListItem)
                    .toList());


        }));
    }

    private Disposable subscribe() {
        var subscription = chatService
                .liveMessages(channelId)
                .subscribe(this::receiveMessages);


        return subscription;


    }
}