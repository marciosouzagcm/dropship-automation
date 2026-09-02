import tkinter as tk
from PIL import Image, ImageTk
from tkinter import messagebox
import requests
import pandas as pd
import webbrowser
import os

# Configurações da API do Mercado Livre
ACCESS_TOKEN = "S12345678-031820-X-12345678"  # Token de acesso
SITE_ID = "MLB"  # Identificador do site do Mercado Livre para o Brasil

import tkinter as tk
from tkinter import messagebox
import requests
import pandas as pd
import webbrowser
import os

# Configurações da API do Mercado Livre
ACCESS_TOKEN = "SUA_CHAVE_AQUI"
SITE_ID = "MLB"

def obter_destaques(termo):
    url = f"https://api.mercadolibre.com/sites/{SITE_ID}/search?q={termo}&access_token={ACCESS_TOKEN}"
    response = requests.get(url)
    return response.json()

def extrair_dados(resp):
    if 'results' in resp:
        dados = []
        for item in resp['results']:
            dados.append({
                'titulo': item['title'],
                'preco': item['price'],
                'url': item['permalink'],
                'imagem': item['thumbnail']
            })
        return dados
    else:
        messagebox.showerror("Erro", "Nenhum item encontrado")
        return []

def pesquisar():
    termo = entrada_termo.get()
    resp_destaques = obter_destaques(termo)
    dados_destaques = extrair_dados(resp_destaques)
    if dados_destaques:
        df = pd.DataFrame(dados_destaques)
        html_content = df.to_html(index=False, classes='table table-striped', border=0, escape=False, formatters={
            'url': lambda x: f'<a href="{x}" target="_blank">Link</a>',
            'imagem': lambda x: f'<img src="{x}" width="50">'
        })
        
        html_template = f"""
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
            <meta charset="UTF-8">
            <title>Pesquisa de Tendências</title>
            <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
            <style>
                body {{
                    font-family: Arial, sans-serif;
                    margin: 20px;
                    background-color: #f0f0f0;
                }}
                
                table {{
                    width: 100%;
                    border-collapse: collapse;
                }}
                
                th, td {{
                    border: 1px solid #ddd;
                    padding: 10px;
                    text-align: left;
                }}
                
                th {{
                    background-color: #f0f0f0;
                }}
                
                .container {{
                    max-width: 800px;
                    margin: auto;
                }}
            </style>
        </head>
        <body>
            <div class="container">
                <h1>Pesquisa de Tendências</h1>
                {html_content}
            </div>
        </body>
        </html>
        """
        
        with open('App_pesquisas.html', 'w', encoding='utf-8') as f:
            f.write(html_template)
        
        webbrowser.open('file://' + os.path.realpath('App_pesquisas.html'))
    else:
        messagebox.showerror("Erro", "Falha ao obter dados")

janela = tk.Tk()
janela.title("Pesquisa de Tendências")
janela.geometry("800x600")

label_termo = tk.Label(janela, text="Termo de pesquisa:", font=("Arial", 14))
label_termo.pack(pady=20)

entrada_termo = tk.Entry(janela, width=50, font=("Arial", 14))
entrada_termo.pack(pady=20)

botao_pesquisar = tk.Button(janela, text="Pesquisar", command=pesquisar, font=("Arial", 14))
botao_pesquisar.pack(pady=20)

janela.mainloop()