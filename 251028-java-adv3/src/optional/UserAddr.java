package optional;

import java.util.Optional;

public class UserAddr {
    static class User {
        private String name;
        private Address address;
        public User(String name, Address address) {
            this.name = name;
            this.address = address;
        }
        public String getName() {
            return name;
        }
        public Address getAddress() {
            return address;
        }
    }
    static class Address {
        private String street;
        public Address(String street) {
            this.street = street;
        }
        public String getStreet() {
            return street;
        }
    }
    public static void main(String[] args) {
        User user1 = new User("name1", new Address("street1"));
        User user2 = new User("name2", null);
        printStreet(user1); // street1
        printStreet(user2); // none
    }
    private static void printStreet(User user) {
        getUserStreet(user).ifPresentOrElse(
                System.out::println,
                () -> System.out.println("none")
        );
    }
    private static Optional<String> getUserStreet(User user) {
        return Optional.ofNullable(user) // Optional<User>
                .map(User::getAddress) // Optional<Address>
                .map(Address::getStreet); // Optional<String>
    }
}
