from Node import Node
from slixmpp.basexmpp import BaseXMPP


D = 20  
class LinkSate(Node):
    def __init__(self, id, password, entity, nodes = None):
        super().__init__(id, password)
        
        self.LS_seqnum = 0
        self.LS = {}
        self.entity = entity
        self.Bxmpp = BaseXMPP()
        self.topology = {}
        self.allNodes = [self.entity]
        self.matriz = []
        self.neighbors = nodes
        self.nick = self.neighbors.keys() if self.neighbors != None else []
        #Function 
        self.shortMtr = None
        self.staticN = self.nick
        
        
    def send(self, to, fromUser):
        self.send_message(to,"<hi>",  tuser = fromUser)

    def eco(self,to, eco_from):
        self.send_message(mto = to,mbody="<eco time='%f'></eco>",mfrom=eco_from)   
        
         
        