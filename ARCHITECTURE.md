# Architecture

### Calculatrice.java

Ce fichier contient le "coeur" de la calculatrice. Il contient tout les élements
permettant de faire fonctionner la calculatrice.

Il contient entre autre :
* Une pile d'`Object`, représentant... la pile (RPM)
* Une liste de `Token` (voir `Token.java` plus bas), qui représente l'historique
* Un set de `Recall.Token` (voir `Token.java` plus bas), qui représente les
variables
* Un dictionnaire (`Map<String,Map<Signature,Operation>>`, `Signature` étant un
type privé interne a la classe representant une liste de `Class`, et voir `Operation`
plus bas), représentant le dictionnaire des opérations disponibles.
* Une fonction d'ajout de données a la pile `addStringToStack()`, prenant en entrée
un `String` qui sera parsé à l'aide de la classe privée interne `TypeParser`, qui
permet à partir d'un `String` de retourner un `Object`.
* Des fonctions de modifications de données (`updateValue()`, `updateVar()`, ...)
(voir `Extension 5`).
* Une fonction de boucle de lecture `start()`, qui parse en boucle dans stdin,
utile pour l'affichage terminal de la Calculatrice.

### Token.java

Token est une classe abstraite scellée, contenant 3 sous types : `OperandToken`,
contenant un unique `Object`, sa "valeur", `OperationToken` qui contient une liste
d'inputs et une `Operation` et calcule sa valeur à partir de ses inputs et enfin
`RecallToken`, un `Token` pointant vers un autre `Token`.
Ils aident a representer l'état d'une cellule dans l'historique.
Tout `Token` à un `SubmissionPublisher`, qui représente sa sortie, et peut
contenir plusieurs receveur. Un `OperandToken` n'a pas d'entrée, `OperationToken`
les a sous formes des inputs et `RecallToken` juste un; le `Token` sur lequel il pointe.

### Operation.java 

`Operation` est une interface contenant une unique méthode `Object compute (Object... args)`

### CalculatriceController.java

Le controller entre la vue et le modèle, rien de bien particulier. Il contient
le main.

### CalculatriceView.java

La vue, rien de bien sensationnel non plus.

## Les extensions : 

### Extension 1 :

Lors de l'entrée d'une chaîne de charactères, on regarde d'abord si le dictionnaire
contient une opération de ce nom. Si oui, alors est ce que l'une des `Signatures`
correspond aux `n` élements au sommet de la pile. Si oui, on retourne un couple
`Object, OperationToken` (voir `Extension 5` pour l'`OperationToken`), et l'`Object`
est empilé.
Sinon, la chaîne tente d'être parsé dans les types connus (défini dans `TypeParser`)
et d'empiler cette valeur, sinon renvoie une erreur.
Les opérations sont ajoutés a la création de la `Calculatrice`, pour chaque types
connu.

# Extension 3 :

Lors du parsing, on vérifie aussi si la valeur du string n'est pas :
* "hist(x)", auquel cas on créer un nouveau `RecallToken` pointant vers la case x de l'historique, et on l'ajoute
à l'historique (et la valeur de la case x de l'historique est empilée).
* "pile(x)", auquel cas on copie juste la valeur d'indice x de la pile dans la pile et l'historique (sous forme
d'`OperandToken`)
* "!x", dépilant le sommet de la pile et l'ajoutant (ou modifiant la valeur) dans le Set, sous format de `RecallToken`
* "?x" empile la valeur stocké par x dans le Set si il existe, et ajoute un `RecallToken` de x dans l'historique.

# Extension 5

L'ajout de la vue fait partie de cette extension.
Seul les variables de l'historique et du tableau des variables sont modifiables.
Lorsque l'on modifie une valeur, alors si elle avait des outputs (qui sont donc des
`Token`), elle force le recalcule de la valeur de ceux ci, qui eux même force le recalcule
de la valeur de leur output etc...
Si l'on modifie une valeur autre qu'un `OperandToken`, alors elle est remplacée dans l'historique
par le nouveau `Token`. Cette opération deconnecte le `Token` remplacé de la chaîne,
en forçant ses inputs (si il en a) de se déconnecter, et le deconnecte aussi de ses outputs,
qui vont maintenant être utilisés par le nouveau `Token` le remplaçant.
Un `Token` ne peut être remplacé par un autre **si et seulement si** les classes
de leurs valeurs sont similaires.
Lors du stockage d'une variable `x`, via `!x`, elle pointe maintenant vers la case
étant en sommet d'historique a ce moment.
Si on modifie cette variable, elle est donc déconnectée de son input.

# Extension 7

Le graph permet d'afficher les opération sur les `Integer` contenant une variable
libre et une variable liée (liée = `OperandToken`). Elle graphe donc x <Opération>
variable libre.
Le graphe affiche toujours l'opération effectuée par le sommet de l'historique,
ou la case d'historique qui à été cliqué (rendu cliquable par le petit symbole
de graphe).
