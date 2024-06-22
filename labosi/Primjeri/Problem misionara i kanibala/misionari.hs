-- |
-- Sveučilište u Zagrebu, Fakultet elektrotehnike i računarstva
-- Umjetna inteligencija
--
-- Pretraživanje prostora stanja: Problem misionara i kanibala
-- v1.0
--
-- Copyright: (c) 2011 Jan Snajder <jan.snajder@fer.hr>
--
-- Zadatak:
-- Napišite program koji ce pretraživanjem u širinu pronaći optimalno
-- rješenje problema misionara i kanibala. Problem je opisan na sljedeći 
-- način: tri misionara i tri kanibala potrebno je jednim camcem prevesti s 
-- jedne strane obale rijeke na drugu, pri cemu se niti u jednom trenutku na
-- jednoj strani obale ne smije naći više kanibala nego misionara. Čamac 
-- može prevesti najviše dvije osobe i ne može ploviti prazan. Optimalno
-- rješenje je ono s najmanjim brojem koraka. Program treba ispisati
-- rješenje u obliku niza operatora i stanja
-- koja dovode do ciljnog stanja.
-- Primjer: http://www.learn4good.com/games/puzzle/boat.htm
--
------------------------------------------------------------------------------

import Data.List
import Control.Monad
import qualified Data.Set as S

------------------------------------------------------------------------------
-- Definicija problema
------------------------------------------------------------------------------

-- Pozicija čamca (lijeva ili desna obala).
data Boat = L | R 
  deriving (Eq,Show,Ord)

-- Stanje je prikazano kao trojka sa sljedećim elementima: broj misionara 
-- na lijevoj obali, broj kanibala na lijevoj obali i pozicija čamca.
type State = (Int,Int,Boat)

-- Početno stanje (tri misionara i tri kanibala su na lijevoj obali).
initState :: State
initState = (3,3,L)

-- Ispitni predikat; vraća True za ciljno stanje.
isGoal :: State -> Bool
isGoal (0,0,R) = True
isGoal _       = False

-- Funkcija za generiranje valjanih sljedbenika zadanog stanja.
nextState :: State -> [State]
nextState (m,c,b) = do 
  (x,y) <- [(1,1),(2,0),(0,2),(1,0),(0,1)]
  let s = if b==L then (m-x,c-y,R) else (m+x,c+y,L)
  guard $ safe s
  return s
  where safe (m,c,_) = (m == 0 || m >= c) && (m == 3 || m <= c)

------------------------------------------------------------------------------
-- Pretraživanje u širinu
------------------------------------------------------------------------------
  
-- Pretraživanje u širinu s pamćenjem posjećenih stanja. Funkcija
-- uzima početno stanje, funkciju sljedbenika i ispitni predikat, a
-- vraća listu stanja od početnog do ciljnog stanja, odnosno praznu
-- listu ako rješenje nije pronađeno.
bfSearch :: (Ord a) => a -> (a -> [a]) -> (a -> Bool) -> [a]
bfSearch s0 succ goal = step [(s0,[])] S.empty
  where step [] v = []
        step (x@(s,ss):xs) v 
          | goal s       = reverse $ s:ss
          | S.member s v = step xs v
          | otherwise    = step (xs ++ expand x) (S.insert s v)
        expand (s,ss) = [(t,s:ss) | t <- succ s]

-- Npr.
-- > bfSearch initState nextState isGoal

-- Pomoćna funkcija za prikaz operatora (prijelaza između dva stanja).
showStep :: State -> State -> String
showStep s1@(m1,c1,b1) s2@(m2,c2,_) = concat $ case b1 of
  L -> [showMC (m1-x,c1-y), " (", showMC (x,y), ") ---> ", showMC (3-m1,3-c1)]
  R -> [showMC (m2-x,c2-y), " <--- (", showMC (x,y), ") ", showMC (3-m2,3-c2)]
  where showMC (m,c) = show m ++ "M " ++ show c ++ "C"
        (x,y) = (abs $ m1-m2,abs $ c1-c2)
        space n = replicate n ' '

solve :: IO ()
solve = putStr . unlines $ zipWith showStep ss (tail ss)
  where ss = bfSearch initState nextState isGoal
