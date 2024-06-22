-- | 
-- Sveučilište u Zagrebu, Fakultet elektrotehnike i računarstva
-- Umjetna inteligencija
--
-- Pretraživanje prostora stanja: Igra brojki
-- v1.0
--
-- Copyright: (c) 2011 Jan Snajder <jan.snajder@fer.hr>
--
-- Zadatak:
-- Napišite program koji koristi pretraživanje u dubinu kako bi
-- riješio problem igre brojki. Cilj igre je, krenuvši od šest zadanih
-- cijelih brojeva, pronaći aritmetički postupak kojim se izvodi neki
-- ciljni broj. Aritmetičke operacije koje se pritom smiju koristiti
-- jesu zbrajanje, oduzimanje, množenje i dijeljenje bez ostatka.
-- Svaki se broj može iskoristiti samo jednom ili niti jednom.
-- Početnih šest brojeva neka zadaje korisnik, a ciljni broj neka se
-- generira slučajno iz intervala od 100 do 999. Program u svakom
-- koraku treba ispisivati trag izvođenja, a na kraju treba ispisati
-- aritmetički izraz koji predstavlja rješenje. Ako izraz kojim se
-- izvodi točan broj nije pronađen, program treba ispisati izraz koji
-- izvodi broj najbliži traženome.  
-- Detaljnije: http://www.crosswordtools.com/numbers-game/faq.php
--
------------------------------------------------------------------------------

import Data.List

------------------------------------------------------------------------------
-- Prikaz aritmetičkog izraza
------------------------------------------------------------------------------

-- Aritmetički izraz prikazan kao algebarski podatkovni tip. Operator
-- je definiran kao zaseban nula-mjesni konstruktor 'Op' jer to
-- pojednostavljuje kod u nastavku. (Preuzeto iz [1].)
data Expr = Val Int | App Op Expr Expr
  deriving (Eq,Show)
data Op = Add | Sub | Mul | Div
  deriving (Eq,Show)

-- Provjera valjanosti operacije: ne dopuštamo (1) dijeljenje s nulom
-- i (2) oduzimanje većeg broja od manjeg. Također sprečavamo
-- redundantne kombinacije koje nastaju uslijed: (1) komutativnosti
-- operatora Add i Mul, (2) množenjem s 1, (3) dijeljenjem s 1.
legal :: Op -> Int -> Int -> Bool
legal Add x y = x <= y
legal Sub x y = x > y
legal Mul x y = 1 < x && x <= y
legal Div x y = 1 < y && x `mod` y == 0

-- Ispis aritmetičkog izraza.
showExpr :: Expr -> String
showExpr (Val v) = show v
showExpr (App op e1 e2) = 
  "(" ++ showExpr e1 ++ showOp op ++ showExpr e2 ++ ")"
  where showOp Add = "+"
        showOp Sub = "-"
        showOp Mul = "*"
        showOp Div = "/"

-- Kombinira dva izraza primjenom svih mogućih legalnih operacija i
-- rezultira listom mogućih izraza.
combineExpr :: (Expr,Int) -> (Expr,Int) -> [(Expr,Int)]
combineExpr (e1,v1) (e2,v2) = 
  [(App op e1 e2, (apply op) v1 v2) | op <- [Add,Sub,Mul,Div],
                                      legal op v1 v2 ]

apply :: Op -> Int -> Int -> Int
apply Add = (+)
apply Sub = (-)
apply Mul = (*)
apply Div = div

------------------------------------------------------------------------------
-- Definicija problema
------------------------------------------------------------------------------

-- Stanje definiramo kao dvojku sastavljenu od (1) izraza koji je u
-- trenutnom "fokusu" izračunavanja i (2) liste neiskorištenih izraza.
-- Na ovaj način možemo prikazati međuizraze potrebne za izračun
-- ciljnog izraza. Kako ne bismo morali svaki puta nanovo izračunavati 
-- vrijednost izraza, svaki izraz prikazujemo kao par 
-- (izraz,vrijednost izraza).
type State = ( (Expr,Int), [(Expr,Int)] )

-- Početno stanje je stanje koje u fokusu ima izraz (Val 0,0), a u
-- listi neiskorištenih izraza izraze sačinjene od početno zadanih
-- projeva iz liste 'ns'.
initState :: [Int] -> State
initState ns = ((Val 0,0) , [(Val v,v) | v <- ns])

-- Pomoćna funkcija koja izabire svaki element iz liste te vraća listu
-- rješenja sastavljenu od parova (izabrani element,preostali elementi).
pickOne :: (Eq a) => [a] -> [(a,[a])]
pickOne xs = [(x,delete x xs) | x <- xs]

-- Funkcija sljedećeg stanja. U početnom stanju jednostavno u fokus
-- izabiremo jedan od neiskorištenih izraza. U ostalim stanjima imamo
-- dvije mogućnosti: (1) kombiniranje izraza u fokusu s nekim od
-- neiskorištenih izraza te brisanje tog izraza iz liste
-- neiskorištenih izraza i (2) kombiniranje dvaju izraza iz liste
-- neiskorištenih izraza te zamjena tako dobivenog izraza s izrazom
-- koji je trenutno u fokusu.
nextState :: State -> [State]
nextState ((Val 0,0),evs) = pickOne evs
nextState (ev,evs) = 
  [(ev3,evs2)    | (ev2,evs2) <- pickOne evs, 
                   ev3 <- combineExpr ev ev2] ++
  [(ev4,ev:evs3) | (ev2,evs2) <- pickOne evs, 
                   (ev3,evs3) <- pickOne evs2, 
                   ev4 <- combineExpr ev2 ev3]

------------------------------------------------------------------------------
-- Pretraživanje u dubinu
------------------------------------------------------------------------------

-- Funkcija za pretraživanje u dubinu. Od klasične izvedbe razlikuje
-- se po tome što ne ispituje je li dosegnut ciljni čvor, već koristi
-- funkciju 'choose' kako bi u svakom čvoru (nakon povratka iz
-- rekurzije) odabrala jedan od dva sljedbenika tog čvora. Ako
-- sljedbenika ima više, uspoređuju se po parovima. Ako se odabere
-- lijevi čvor, desni se ne treba evaluirati, čime se zaustavlja
-- daljnje pretraživanje. Na ovaj način možemo naći najbolje rješenje,
-- ukoliko egzaktno rješenje ne postoji.
dfSearch :: a -> (a -> [a]) -> (a -> a -> a) -> a 
dfSearch s succ choose = case succ s of
   [] -> s
   ss -> foldr1 choose [dfSearch s succ choose | s <- ss]

-- Funkcija koja, s obzirom na zadani ciljni broj 'n', odabire bolje
-- od dva stanja. Ako lijevo stanje sadržava izraz s ciljnim brojem,
-- odabire se to stanje i lijevo stanje se ne evaluira (~ indicira
-- tzv. "lijeni uzorak"). U protivnom se odabire stanje s izrazom čija
-- je vrijednost bliža traženom broju.
better :: Int -> State -> State -> State
better n s1@((_,v1),_) ~s2@((_,v2),_) 
  | v1 == n                 = s1
  | abs (n-v1) < abs (n-v2) = s1
  | otherwise               = s2

-- Uzima zadanu listu brojki i traženi broj te poziva pretraživanje u
-- dubinu i ispisuje rezultat.
solve :: [Int] -> Int -> IO ()
solve ns n = putStrLn $ showExpr e ++ " = " ++ show v
 where ((e,v),_) = dfSearch (initState ns) (nextState) (better n)

-- Npr.
-- > solve [1..6] 288
-- ((((1+2)*5)-3)*(4*6)) = 288
-- > solve [75,25,50,5,4,4] 947
-- (((4*5)*(75-25))-(4+50)) = 946

-- Gornje rješenje eksplicitno je oblikovano kao problem pretraživanja 
-- prostora stanja. Zainteresirani za sofisticiranije (čitaj: više 
-- funkcijsko) rješenje upućuju se na [1] ili poglavlje 20 u [2].
--
-- [1] Graham Hutton. The countdown problem. Journal of Functional
--     Programming, 12(6):609-616, Cambridge University Press, 2002.
--     http://www.cs.nott.ac.uk/~gmh/bib.html#countdown
-- [2] Richard Bird. Pearls of Functional Algorithm Design. Cambridge
--     University Press, 2010.

