from node import Node
from slixmpp.basexmpp import BaseXMPP
import json
import asyncio
from scipy.sparse.csgraph import shortest_path
import numpy as np
from xml.etree import ElementTree as ET
from time import time

De = 20  
class LinkSate(Node):
    def __init__(self, id, password, entity, nodes = None):
        super().__init__(id, password)
        
        self.LS_seqnum = 0
        self.LS = {}
        self.entity = entity
        self.Bxmpp = BaseXMPP()
        self.toplgy = {}
        self.allNodes = [self.entity]
        self.matriz = []
        self.neighbors = nodes
        self.nick = self.neighbors.keys() if self.neighbors != None else []
        self.create_topology()
        self.shortMtr = None
        self.staticN = self.nick
        
    def create_topology(self):
        self.LS['node'] = self.entity
        self.LS['seq'] = self.LS_seqnum
        self.LSA['age'] = None
        self.LSA['weights'] = {}
        print("Neighbors", self.nick)
        for node in self.nick:
            self.LS['weights'][node] = 5
        self.toplgy[self.LSA['node']] = self.LSA        
        
    

    def send(self, to, fromUser):
        self.send_message(to,"<hi>",  tuser = fromUser)

    def eco(self,to, eco_from):
        self.send_message(mto = to,mbody="<eco time='%f'></eco>",mfrom=eco_from)   
        
    def update_topology(self, node, weight):
        self.LSA['weights'][node] = weight
        
    def send_topology(self, to):
        self.LS_seqnum += 1
        self.LS['seq'] = self.LSA_seqnum
        #self.LS['age'] = time()
        self.toplgy[self.LS['node']] = self.LS
        lsa_json = json.dumps(self.LS)
        self.send_message(to, "<pack lsa='%s'></pack>" % lsa_json, mfrom=self.boundjid)
        
    #dijkstra algorithm
    def shortestP(self):
        path = []
        return path.reverse()
    
    def dijkstra(self):
        if len(self.matriz) >= 1:
            D, Pr = shortest_path(self.matriz, directed=True, method='D', return_predecessors= True)
            self.shMatrix = Pr
            
    async def update_table(self):
        while True:
            for router in self.nick:
                self.eco(self.neighbors[router], self.boundjid)
            await asyncio.sleep(5)
            for router in self.nick:
                self.send_topology(self.neighbors[router])
            self.dijkstra()
            
    def get_nickname(self, id):
        keyL = list(self.neighbors.keys())
        if id not in self.neighbors.values():
            return 
        valL = list(self.neighbors.values())
        return keyL[valL.index(id)]
    
    def i_listener(self):
        self.loop.create_task(self.update_table())

    def flood(self, to, package):
        self.send_message(to, "<pack ls='%s'></pack>" % package, mfrom=self.boundjid)

    def sendMessage(self, to, msg):
        path = self.shortestP(to)
        print("%s: The shortest path: %s" %(self.entity,path))
        if len(path) > 1:
            self.send_message(mto=self.neighbors[path[1]],mbody="<msg chat='%s' to='%s' ></msg>" %(msg, to),mfrom=self.boundjid)
            
    def updateMatrix(self):
        length = len(self.allNodes)
        self.matriz = np.zeros((length, length), dtype=np.float16)
        for row_node in self.allNodes:
            for col_node in self.toplgy[row_node]['weights'].keys():
                row = self.allNodes.index(row_node)
                if col_node in self.allNodes:
                    col = self.allNodes.index(col_node)
                else:
                   return
                self.matriz[row][col] = self.toplgy[row_node]['weights'][col_node]
                
    def parse_path(self, path):
        return [self.allNodes[i] for i in path]
    
  
    def get_shortest_path(self, destiny): 
        _from = self.allNodes.index(self.entity)
        destiny = self.allNodes.index(destiny)
        path = [destiny]
        k = destiny
        while self.shortMtr[_from, k] != -9999:
            path.append(self.shortMtr[_from, k])
            k = self.shortMtr[_from, k]
        return self.parse_path(path[::-1]) 

    async def Message(self, message):
        if message['type'] in ('normal', 'chat'):
            if message['body'][:7] in ("<hello>"):
                message.reply(self.boundjid).send()
                print("Receive message from neighbor, send answer")
            elif message['body'][1:4] == "eco":
                xml_parse = ET.fromstring(message['body'])
                timestamp = float(xml_parse.attrib['time'])
                if self.is_offline:
                    timestamp -= 100
                    message.reply("<a_eco time='%s'></a_eco>" % str(timestamp)).send()
                    
            elif message['body'][1:6] == "a_eco":
                pack_from = message['from'].bare
                node_entity = self.nick(pack_from)
                end_time = time()
                msg_parse = ET.fromstring(message['body'])
                start_time = float(msg_parse.attrib['time'])
                delta_time = (end_time - start_time) / 2
                delta_time = round(delta_time, 1)
                self.update_topology(node_entity, delta_time)
                
            elif message['body'][1:5] == "pack":
                parse = ET.fromstring(message['body'])
                pack_json = parse.attrib['lsa']
                lsa = json.loads(pack_json)
                n_entity = lsa['node']
                if lsa['node'] not in self.toplgy.keys():
                    self.toplgy[lsa['node']] = lsa
                    for neighbor in self.nick:
                        if neighbor != n_entity:
                            self.flood(self.neighbors[neighbor], json.dumps(lsa))
                    if lsa['node'] not in self.allNodes:
                        self.allNodes.append(lsa['node'])
                        self.allNodes.sort()
                    self.updateMatrix() 
                else:
                    try:
                        d_time = float(lsa['age']) - float(self.toplgy[lsa['node']]['age']) 
                    except TypeError as e:
                        pass
                    if self.topo[lsa['node']]['seq'] >= lsa['seq']:
                        if d_time > De:
                            self.toplgy[lsa['node']] = lsa
                            for neighbor in self.nick:
                                if neighbor != n_entity:
                                    self.flood(self.neighbors[neighbor], json.dumps(lsa))
                        else:
                            pass
                    else:
                        self.toplgy[lsa['node']] = lsa
                        for neighbor in self.nick:
                            if neighbor != n_entity:
                                self.flood(self.neighbors[neighbor], json.dumps(lsa))
                        self.updateMatrix()
                        
            elif message['body'][1:4] == "msg":
                msg_parse = ET.fromstring(message['body'])
                bare_msg = msg_parse.attrib['chat']
                msg_to = msg_parse.attrib['to']
                if msg_to != self.entity:
                    self.send_msg(msg_to, bare_msg)
                else:
                    print("Receive messages: %s" % bare_msg)
            else:
                pass
                    

            


            
        
    
    
    

   
        