package com.asadaclan.accounting.parsers;

import java.util.Arrays;
import java.util.List;

import com.asadaclan.accounting.models.Transaction;

public class TransactionParser implements Parser<Transaction> {
    @Override
    public Transaction parse(String[] splittedMessage) {
        try {
            List<String> messageParts = Arrays.asList(splittedMessage);

            if (messageParts.size() < 3) {
                // message should contain atleast 1) messageAction, 2) item(s) , 3) dollar
                // amount
                throw new ParseMessageException("Not enought information");
            }

            MessageAction action = MessageAction.;
            Optional<String> transactionId = Optional.empty();
            Optional<LocalDate> date = Optional.empty();
            String items = "";
            Float transactionAmount = 0.0f;
            Optional<Float> tip = Optional.empty();
            // Optional<Image> image;
            MessageComponents messageComponents = MessageComponents.MessageAction;
            int position = 0;
            while (position < splittedMessage.size()) {
                switch (messageComponents) {
                    case MessageComponents.MessageAction:
                        action = MessageAction.valueOf(splittedMessage.get(position));
                        position++;
                        messageComponents = MessageComponents.TransactionId;
                        break;
                    case MessageComponents.TransactionId:
                        if (splittedMessage.get(position).startsWith("trx")) {
                            transactionId = Optional.of(splittedMessage.get(position));
                            position++;
                        }
                        messageComponents = MessageComponents.Date;
                        break;
                    case MessageComponents.Date:
                        date = parseDate(splittedMessage.get(position));
                        if (date.isPresent()) {
                            position++;
                        }
                        messageComponents = MessageComponents.Items;
                        break;
                    case MessageComponents.Items:
                        items = splittedMessage.get(position);
                        position++;
                        messageComponents = MessageComponents.TransactionAmount;
                        break;
                    case MessageComponents.TransactionAmount:
                        transactionAmount = Float.valueOf(splittedMessage.get(position));
                        position++;
                        messageComponents = MessageComponents.Tip;
                        break;
                    case MessageComponents.Tip:
                        try {
                            tip = Optional.of(Float.valueOf(splittedMessage.get(position)));
                            position++;
                        } catch (NumberFormatException ignore) {
                        }
                        messageComponents = MessageComponents.Image;
                        break;
                    case MessageComponents.Image:
                        System.out.println("NOT IMPLEMENTED");
                    default:
                        throw new RuntimeException("how did you manage to do this??");
                }
            }
            return new ReceivedMessage(action, transactionId, date, items, transactionAmount, tip);
        } catch (Exception exception) {
            throw new ParseMessageException("Unable to parse message.");
        }
    }

}
