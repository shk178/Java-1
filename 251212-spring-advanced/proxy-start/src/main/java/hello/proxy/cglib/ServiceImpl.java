package hello.proxy.cglib;

public class ServiceImpl implements ServiceInterface {
    @Override
    public void save() {
        System.out.println("ServiceImpl.save");
    }

    @Override
    public void find() {
        System.out.println("ServiceImpl.find");
    }
}
