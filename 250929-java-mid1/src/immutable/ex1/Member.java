package immutable.ex1;

import immutable.ImmutableAddress;

public class Member {
    private String name;
    private ImmutableAddress address;
    public Member(String name, ImmutableAddress address) {
        this.name = name;
        this.address = address;
    }
    public ImmutableAddress getAddress() {
        return address;
    }
    public void setAddress(ImmutableAddress address) {
        this.address = address;
    }
    @Override
    public String toString() {
        return "Member{" +
                "address=" + address +
                ", name='" + name + '\'' +
                '}';
    }
}
