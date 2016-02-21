package io.vertx.examples.todomvc.dao.impl;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.examples.todomvc.dao.TodoDAO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static io.vertx.examples.todomvc.Server.*;

public class LocalMapDAO implements TodoDAO {

    private final static String TODO_MAP = "TODOS";
    private final Vertx vertx;
    private final AtomicInteger counter = new AtomicInteger();

    public LocalMapDAO(Vertx vertx) {
        this.vertx = vertx;
    }

    private LocalMap<Integer, JsonObject> getMap() {
        return vertx.sharedData().getLocalMap(TODO_MAP);
    }

    @Override
    public List<JsonObject> getTodos() {
        Collection<JsonObject> col = getMap().values();
        List<JsonObject> list = new ArrayList<>(col);
        list.sort((todo1, todo2) -> {
            return Integer.compare(todo1.getInteger("id"), todo2.getInteger("id"));
        });
        return list;
    }

    @Override
    public JsonObject getTodo(Integer id) {
        return getMap().get(id);
    }

    @Override
    public JsonObject updateTodo(Integer id, JsonObject diff) {
        JsonObject oldTodo = getTodo(id);
        if (oldTodo == null) return null;
        JsonObject newTodo = oldTodo.mergeIn(diff);
        getMap().put(id, newTodo);
        return newTodo;
    }

    @Override
    public JsonObject deleteTodo(Integer id) {
        JsonObject oldTodo = getTodo(id);
        if (oldTodo != null) {
            getMap().remove(id);
        }
        return oldTodo;
    }

    @Override
    public JsonObject newTodo(JsonObject todo) {
        int id = counter.incrementAndGet();
        todo.put("id", id);
        todo.put("completed", false);
        todo.put("url", "http://" + HOST + ":" + PORT + "/todos/" + id);
        getMap().put(id, todo);
        return todo;
    }

    @Override
    public void clearTodos() {
        getMap().clear();
    }

}
