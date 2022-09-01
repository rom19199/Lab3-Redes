import slixmpp
import logging
from getpass import getpass
from argparse import ArgumentParser
from slixmpp.exceptions import IqError, IqTimeout

#https://slixmpp.readthedocs.io/en/slix-1.5.1/_modules/slixmpp/basexmpp.html
logging.basicConfig(level=logging.DEBUG, format="%(levelname)-8s %(message)s")

class Client(slixmpp.ClientXMPP):
#  Here we s to describe all the things we need
  def __init__(self, userid, password):
    slixmpp.ClientXMPP.__init__(self, userid, password)
    self.userid = userid
    self.password = password
    self.add_event_handler("session_s", self.sessions)
    self.add_event_handler("register", self.registrar)
    self.add_event_handler("message", self.Alertas)

  async def sessions(self, e):
    self.send_presence()
    await self.get_roster()

# Here we tried to search all the information of the contact
    def contacto():
      print("\n")
      contacts = self.client_roster.groups()
      
      for contact in contacts:
        print("Contactos")
        for jid in contacts[contact]:
          user = self.client_roster[jid]['name']
          if self.client_roster[jid]['name']:
            print('\n', user, ' (',jid,')')
          else:
            print('\n', jid)

          connecciones = self.client_roster.presence(jid)
          for res, pres in connecciones.items():
            show = 'available'
            if pres['show']:
              show = pres['show']
            print('   - ',res, '(',show,')')
            if pres['status']:
              print('       ', pres['status'])
      print("")
      

#https://xmpp.readthedocs.io/en/latest/tutorial.html#client-tcp-connection
    def nuevo():
      nuevo = input("Ingrese el Usuario: ")
      self.send_presence_subscription(pto=nuevo)
      mensaje=" Lloremos por el proyecto"
      self.send_message(para=nuevo, historia=mensaje, tipo="chat", de=self.boundjid.bare)


    def informacion():
      self.get_roster()
      usedidContact = input("Ingrese el ID: ")
      user = self.client_roster[usedidContact]['name']
      print('\n %s (%s)' % (user, usedidContact))

      connecciones = self.client_roster.presence(usedidContact)
      if connecciones == {}:
        print('       Away')
      for res, pres in connecciones.items():
        show = 'available'
        if pres['show']:
          show = pres['show']
        print('   - ', res, ' - ', show)
        print('       ',  pres['status'])

    def avisar(emisor, state):
      self.register_plugin("xep_0085")
      message = self.Message()
      message["estado"] = state
      message["to"] = emisor

      message.send()

# Here we show the messagge
    def MensajeD():
      canal = input("A quien le deseas enviar un mensaje? ")
      mensaje = input("Mensaje: ")
      self.register_plugin("xep_0085")
      self.send_message(para=canal, historia=mensaje, tipo="chat")
      avisar(canal, 'paused')
      print("Envio Exitoso")
# Here its about the status of the contact
    def nuevoE():
      o1 = input(" Estado1 ")
      o2= input(" Estado2 ")
      self.send_presence(pshow=o1, pstatus=o2)
      print("Nuevo Estado")
    
    print("Inicio")
    inicio = True
    while inicio:
      print("1.  Usuarios 2.  Añadir usuario 3.  Info Usuario 4.  Enviar Mensaje 5.  Chat grupal 6.  Estado 7.  Archivo 8.  Eliminar la cuenta 9.  Salir ")
      opcion = int(input("Que opcion desea realizar? "))
      if opcion == 1:
        contacto()
      elif opcion == 2:
        nuevo()
      elif opcion == 3:
        informacion()
      elif opcion == 4:
        contacto()
        MensajeD()
      elif opcion == 6:
        nuevoE()
      elif opcion == 8:
#           In all of this plugiins i took it by : https://slixmpp.readthedocs.io/en/latest/api/plugins/index.html
        self.register_plugin('xep_0030') 
        self.register_plugin('xep_0004')
        self.register_plugin('xep_0077')
        self.register_plugin('xep_0199')
        self.register_plugin('xep_0066')

        eliminar = self.Iq()
        eliminar['type'] = 'set'
        eliminar['from'] = self.boundjid.user
        eliminar['register']['remove'] = True
        print('Eliminado')
        eliminar.send()
        self.disconnect()
      elif opcion == 9:
        self.disconnect()
        inicio = False
      else:
        print("Seleccionar opcion")

  def Alertas(self, message):
    print(str(message["from"]), ":  ", message["body"])

  async def registrar(self, iq):
    self.send_presence()
    self.get_roster()
    resp = self.Iq()
    resp['type'] = 'set'
    resp['register']['username'] = self.boundjid.user
    resp['register']['password'] = self.password
    try:
      await resp.send()
      logging.info("Nueva Cuenta para %s!" % self.boundjid)
    except IqError as e:
      logging.error("Erro, no se pudo registrar: %s" % e.iq['error']['text'])
      self.disconnect()
    except IqTimeout:
      logging.error("Error.")
      self.disconnect()

def registrar(userid, password):
  cliente = Client(userid, password)
  cliente.register_plugin("xep_0030")
  cliente.register_plugin("xep_0004")
  cliente.register_plugin("xep_0077")
  cliente.register_plugin("xep_0199")
  cliente.register_plugin("xep_0066")

  cliente["xep_0077"].force_registration = True

  cliente.connect()
  cliente.process(forever=False)


  
def iniciarSesion(userid, password):
  cliente = Client(userid, password)
  cliente.register_plugin("xep_0030")
  cliente.register_plugin("xep_0199")
  cliente.connect()
  cliente.process(forever=False)

s = True

while s:
  print("1. Registrar 2. Iniciar Sesion 3. Salir del proyecto")

  O = int(input("Seleccione una opcion "))
  if O == 1:
    userid = input("Ingrese usuario: ")
    password = input("Ingrese contrseá: ")
    registrar(userid, password)
  elif O == 2:
    userid = input("Ingrese ID: ")
    password = input("Ingrese la contraseña: ")
    iniciarSesion(userid, password)
  elif O == 3:
    print("Gracias")
    s = False
  else:
    print("Seleccione la opcion que prefiera")

#Esperemos que funcione"
# References:
# https://slixmpp.readthedocs.io/en/latest/getting_sed/echobot.html
#https://slixmpp.readthedocs.io/en/latest/api/plugins/xep_0004.html
# https://slixmpp.readthedocs.io/_/downloads/en/slix-1.5.1/pdf/
# https://github.com/fritzy/SleekXMPP
