# MatchRevisions
**Prevents client-server desync problems**

# What is Revisions?

It is internal number that screen uses to check if it should send all the contents or not.

1.17 changes made server send everything, yes, even if client has processed it. It caused a lot of desync problems.. but simply it could be prevented by using and matching 'revisions'.

But this mod is in development - it only handles certain types of actions. In some weird modded server, like if you're prevented to interact some inventory as if you're in spectator, it could cause desync again.


# How? and When?
If you swaps item too fast or, you have bad ping, server's response will rubberband you.

Consider this situation : you placed stone, then swapped to cobblestone, then placed, then swap to stone.

With bad ping, or even with good ping this can happen:

1. Server listens and sends respond : you swapped to cobblestone.

2. When you swap to stone, but server responded, and response about 'swap to stone again' has not recieved, you'll see cobblestone in hand.

3. This could lead to bad situations : you try to swap again with stone, but actually in server you're already holding stone.

4. Then you'll see cobblestone in hand, finally.

This happens because Server also process actions, ask mojank about WHY should server process actions even if client DO process them.

So this mod simply just tries to match 'revision', which is never updated in client side actively, WHY MOJANG?

Then rejects late responses about previous ones.


# But some problems... might exist?

Particually, Hopper might have active connection - and this mod can't help about it, it does not improve connection speed.

Also, Villagers... might have sync with server, because client does not process actions actively in this case. This is just small fraction of how mojang's inconsistent bad code works.

But in normal conditions, you'll get less or no desyncs with this mod... at least, this can help EasyPlace or Item Scroller.

# How to build instead of suspiscious jar?

Download code zip then run `gradlew build`.

If you need another versions, then change `gradle.properties` - fabric loader and minecraft version.

# Issue tracker

This mod is in just preview, but it just works for some cases... 

# Known issues

Swap key does not use interactionManager, how...?
