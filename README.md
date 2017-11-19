# FEUP for Students

Uma aplicação para aceder ao site do SIGARRA da Faculdade de Engenharia da Universidade do Porto, optimizado para telemóveis.

## Download
<a href="https://play.google.com/store/apps/">
  <img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
       alt="Android app on Google Play" width="200"/>
</a>

## Feautures
* Vizualização e navegação optimizadas para telemóvel
* Login automático
* Adicionar favoritos
 
## Segurança 
Nenhum dados são retirados do site do Sigarra, ao carregar a página a aplicação aplica um ficheiro CSS costumizado que muda o estilo da página. 
O Auto-login corre se a aplicação verificar que o utilizador não se encontra com sessão iniciada, e utiliza os dados encriptados introduzidos pelo utilizador para fazer login através de um simples script de JavaScript.
Os dados introduzidos são todos encriptados no dispositivo e nunca saem do telémovel. A encriptação é tratada pela biblioteca [Qlassified Android](https://github.com/Q42/Qlassified-Android) que é um wrapper do [Keystore System](https://developer.android.com/training/articles/keystore.html) do Android. O criador desta aplicação não tem qualquer acesso as estes dados.

## Bibliotecas
 * [AHBottomNavigation](https://github.com/aurelhubert/ahbottomnavigation) -  aurelhubert
 * [MaterialDrawer](https://github.com/mikepenz/MaterialDrawer) - mikepenz
 * [LovelyDialog](https://github.com/yarolegovich/LovelyDialog) - yarolegovich
 * [Qlassified Android](https://github.com/Q42/Qlassified-Android) - Q42

## Contribuir
O código da aplicação encontra-se disponivel na sua totalidade neste repositório, podes contribuir e submeter pull requests pelo o GitHub ou enviar sugestões e feedback para pipas.software@gmail.com

## Autor
 * **Paulo Correia** - MIEIC FEUP
