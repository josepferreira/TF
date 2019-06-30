import sys
import matplotlib.pyplot as plt
import numpy as np
import json

args = len(sys.argv)

ficheiros = []
for i in range(1,args-1):
    ficheiros.append(sys.argv[i])

label = sys.argv[args-1]

def read(nome):
    """
    Método que le um ficheiro do armazenamento local.
    Devolve um objeto JSON com o username, a timeline e a lista dos utilizadores que o utilizador atual segue.
    """
    
    data = {}
    try:
        with open(nome, 'r') as f:
            data = json.load(f)

    except FileNotFoundError:
        pass
    finally:
        return data

def leFicheiro(nome):
    dados = read(nome)
    x = []
    medias = []
    tps = []
    chaves = [int(i) for i in dados.keys()]
    for k in sorted(chaves):
        x.append(k)
        aux = np.array(dados[str(k)])
        media = np.mean(aux)
        tp = len(aux) / ( np.sum(aux) ) * 1000
        medias.append(media)
        tps.append(tp)
    
    return (x,medias,tps)

def criaGrafico(x,y,label, ylabel):
    for i in y:
        plt.plot(x,i)
    
    legend = []
    for i in range(0,len(y)):
        legend.append(str(i+1) + 'ª versao')

    plt.legend(legend, loc='upper left')

    plt.title(label)
    plt.xlabel('Nr. clientes concorrentes')
    plt.ylabel(ylabel)
    plt.show()

medias = []
tps = []
for fich in ficheiros:
    (x, media, tp) = leFicheiro(fich)
    medias.append(media)
    tps.append(tp)

criaGrafico(x,medias,label+' (média)', 'Tempo de resposta (ms)')
criaGrafico(x,tps,label+' (throughput)', 'Nr. operações por segundo')

