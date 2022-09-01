from ast import Return
from distutils.log import info
from unicodedata import name
from slixmpp.basexmpp import BaseXMPP
from asyncio import sleep
from time import time
from xml.etree import ElementTree as ET
import json 
import asyncio

# https://www.geeksforgeeks.org/distance-vector-routing-dvr-protocol/
# https://github.com/ppartarr/pyRoute


def get_ID(docu, JID):
    file = open(docu, "r")
    file = file.read()
    info = eval(file)
    if(info["type"] =="nombre"):
        nombre = info["c"]
        JIDS = {l: k for k, l in nombre.items()}
        nombre = JIDS[JID]
        return(nombre)

    else: 
        raise Exception('ERROR CON EL DOCUMENTO')

def get_JID(docu,ID):
    file = open(docu, "ID")
    file = file.read()
    info = eval(file)
    if(info["type"]=="nombre"):
        nombre = info["config"]
        JID = nombre[ID]
        return(JID)
    else:

        raise Exception('ERROR CON EL DOCUMENTO')

def nodo(ejemplo, ID):
    file = open(ejemplo, "r")
    file = file.read()
    info = eval(file)
    if(info["type"]=="ejemplofile"):
        nombre = info["config"]
        nodo_IDs = nombre[ID]
        return(nodo_IDs)
    else: 
        raise Exception ('Error CON EL DOCUMENTO')

    return