fix8on
======

A FIX application designed as a reference app for JDK 8. This is a teaching aid and reference app.
It is not necessarily designed for robustness or production deployments. You use it in PROD at your
own risk. 

It uses QuickFIX as its underlying FIX library, and acts as a direct-market access (DMA) engine.

For the non-financial specialist, this means it listens on a socket for incoming messages using the
FIX protocol (http://www.fixprotocol.org/). These can be thought of as incoming orders from 
clients which the clients want to place on an exchange or other electronic market. 

In general, most companies are not allowed to directly send orders into an electronic market.
Instead, only members are allowed to place orders. So intermediaries (brokers) place the orders 
for clients. 

So, the direct-market access application is effectively an end-point for client orders. The received
orders may need to be transformed and filtered before they are placed on the market.

For example:

* Client may use a different set of symbols to represent the instrument being ordered than the market does.
(E.g. client may use Bloomberg or Reuters symbols, whereas the market uses a specific set of symbols).
* DMA engine must ensure that client doesn't breach risk limits
* Client may use different decimal place precision or quote in pence instead of pounds

So this style of application is well suited to a pipeline design utilising the basic building blocks
of java.util.functions

Individual clients are configured up by using a JSON format to describe them.

Build the application with:

    ant jar

Start the application with:

    ant exec

