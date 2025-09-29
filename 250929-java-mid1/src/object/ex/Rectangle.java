package object.ex;

import java.util.Objects;

public class Rectangle {
    int width;
    int height;
    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }
    @Override
    public String toString() {
        return "Rectangle{width=" + this.width +
                ", height=" + this.height +
                "}";
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        Rectangle r = (Rectangle) obj;
        return (this.width == r.width) && Objects.equals(this.height, r.height);
    }
}
