package it.unibo.donkeykong.ecs.component;

/**
 * Graphic component, the graphical representation of an entity.
 *
 * @param id the texture type, to be used to retrieve the texture from the texture manager
 * @param width the width
 * @param height the height
 */
public record Graphic(String id, double width, double height) implements Component {}
