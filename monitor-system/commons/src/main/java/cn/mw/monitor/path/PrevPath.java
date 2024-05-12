package cn.mw.monitor.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PrevPath {
    public static final String PATH_SEP= ",";
    private String path;
    private List<List<GNode>> releatedPath = new ArrayList<>();
    private int score;

    public PrevPath(String path){
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<List<GNode>> getReleatedPath() {
        return releatedPath;
    }

    public void setReleatedPath(List<List<GNode>> releatedPath) {
        this.releatedPath = releatedPath;
    }

    public void addReleatedPath(List<GNode> path){
        this.releatedPath.add(path);
    }

    public String[] getPathArray(){
        return path.split(PATH_SEP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrevPath prevPath = (PrevPath) o;
        return Objects.equals(path, prevPath.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return "PrevPath{" +
                "path='" + path + '\'' +
                ", score=" + score +
                '}';
    }
}
