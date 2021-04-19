public class User {

    private ChatFunctions chat;
    private String name = "Person 1";
    private boolean isLogged = false;

    public User(ChatFunctions chat) {
        this.chat = chat;
    }

    public void sendMessage(String message) {
        if (isLogged) {
            chat.sendMessage(message);
            System.out.println(message);
        } else if (message.startsWith("&login ")) {
            login(message);
        } else {
            System.out.println("Type '&login <nickname>' to login");
        }
    }

    public void login(String message) {
        if (!isLogged) {
            // '&login <nickname>', it takes <nickname>
            String nickname = message.split(" ")[1];
            isLogged = chat.login(nickname);
            if (isLogged) {
                System.out.println("Connected!");
            } else {
                System.out.println("This nickname already exist, try again.");
            }
        }
    }
}
