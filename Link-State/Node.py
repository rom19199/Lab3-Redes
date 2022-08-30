#Class to initialze nodes

import asyncio
import logging
import slixmpp
from aioconsole import aprint
import xml.etree.ElementTree as ET
from slixmpp.exceptions import IqError, IqTimeout

class Node(slixmpp.ClientXMPP):
    def __init__(self, id, password, nick=None, newUser=False, t_keys=None):
        super().__init__(id, password)
        
        if not nick:
            self.nickname = id.split('@')[0]
        else:
            self.nickname = nick
            
        self.connected_event = asyncio.Event()
        self.topo_keys = t_keys
        
        self.add_event_handler("session_start", self.Start)
        self.add_event_handler("register", self.register)
        self.add_event_handler("message", self.Message)
        self.add_event_handler("message", self.disconnect)
        
        self.register_plugin("xep_0030")
        self.register_plugin("xep_0004")
        self.register_plugin("xep_0199")
        self.register_plugin("xep_0066")
        self.register_plugin("xep_0045")
        self.register_plugin("xep_0085")
        self.register_plugin("xep_0096")
        self.register_plugin("xep_0059")
        self.register_plugin("xep_0060")
        self.register_plugin("xep_0071")
        self.register_plugin("xep_0128")
        self.register_plugin("xep_0363")
        
        if newUser:
            self.register_plugin('xep_0077')
            self.register_plugin('xep_0004')
            self.register_plugin('xep_0066')
            self['xep_0077'].force_registration = True
        self.is_offline = False
        
        
    async def register(self, iq):
        self.send_presence()
        self.get_roster()
        resp = self.Iq()
        resp['type'] = 'set'
        resp['register']['username'] = self.boundjid.user
        resp['register']['password'] = self.password
        
        try:
            await resp.send()
            logging.info("Account created for %s!" % self.boundjid)
        except IqError as e:
            logging.error("Could not register account: %s" % e.iq['error']['text'])
            self.disconnect()
        except IqTimeout:
            logging.error("No response from server.")
            self.disconnect()

    async def Start(self, event):
        
        try:
        #Presence
            self.send_presence() 
        #List of contacts
            await self.get_roster()
            self.connected_event.set()
            
        except IqError as err:
            print('Error: %s' % err.iq['error']['condition'])
        except IqTimeout:
            print('Error: Request timed out')
            
    async def Message(self, message):
        if message['type'] in ('normal', 'chat'):
            await aprint("\n{}".format(message['body']))

    def disconnect(self, e):
        print('Node {} is offline'.format(e['from'].bare))
      