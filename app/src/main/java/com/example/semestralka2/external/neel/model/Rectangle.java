package com.example.semestralka2.external.neel.model;

public class Rectangle {

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(Point p) {
        return p.x >= x &&
               p.x < x + width &&
               p.y >= y &&
               p.y < y + height;
    }

    public boolean intersects(Rectangle other) {
        return other.x < x + width &&
           other.x + other.width > x &&
           other.y < y + height &&
           other.y + other.height > y;
    }
}