from Node import Node
from slixmpp.basexmpp import BaseXMPP
import json
import asyncio
from scipy.sparse.csgraph import shortest_path

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
            
        
    
    
    

   
        