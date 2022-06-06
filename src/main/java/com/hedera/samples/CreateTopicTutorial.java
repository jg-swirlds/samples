package com.hedera.samples;

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class CreateTopicTutorial {
    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException, InterruptedException {

        //Grab your Hedera testnet account ID and private key
        AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
        PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY"));

        //Build your Hedera client
        Client client = Client.forTestnet();
        client.setOperator(myAccountId, myPrivateKey);

        //Create a new topic
        TransactionResponse txResponse = new TopicCreateTransaction()
                .execute(client);

        //Get the receipt
        TransactionReceipt receipt = txResponse.getReceipt(client);

        //Get the topic ID
        TopicId topicId = receipt.topicId;

        //Log the topic ID
        System.out.println("Your topic ID is: " +topicId);

        // Wait 5 seconds between consensus topic creation and subscription creation
        Thread.sleep(5000);

        //Subscribe to the topic
        new TopicMessageQuery()
                .setTopicId(topicId)
                .subscribe(client, resp -> {
                    String messageAsString = new String(resp.contents, StandardCharsets.UTF_8);
                    System.out.println(resp.consensusTimestamp + " received topic message: " + messageAsString);
                });

        //Submit a message to a topic
        TransactionResponse submitMessage = new TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage("hello, HCS!")
                .execute(client);

        //Get the receipt of the transaction
        TransactionReceipt receipt2 = submitMessage.getReceipt(client);

        //Wait before the main thread exits to return the topic message to the console
        Thread.sleep(30000);

    }
}