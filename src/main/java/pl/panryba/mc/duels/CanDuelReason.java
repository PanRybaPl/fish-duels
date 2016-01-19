/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.duels;

/**
 *
 * @author PanRyba.pl
 */
public enum CanDuelReason {
    ALLOWED,
    SAME_PLAYER,
    BLOCKED,
    YOU_ARE_IN_DUEL,
    OTHER_IN_DUEL,
    ALREADY_IN_DUEL,
    OTHER_NOT_FOUND,
    YOU_ARE_NEWBIE_PROTECTED,
    OTHER_IS_NEWBIE_PROTECTED,
    NOT_ENOUGH_EMPTY_SLOTS
}
