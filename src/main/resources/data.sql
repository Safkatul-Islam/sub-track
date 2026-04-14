INSERT INTO subscriptions (name, price, renewal_date, category) VALUES
    ('Netflix',              15.99, DATEADD('DAY', 10, CURRENT_DATE), 'STREAMING'),
    ('Spotify',                9.99, DATEADD('DAY',  5, CURRENT_DATE), 'MUSIC'),
    ('Gym Membership',        40.00, DATEADD('DAY', 20, CURRENT_DATE), 'FITNESS'),
    ('Adobe Creative Cloud',  20.99, DATEADD('DAY',  2, CURRENT_DATE), 'SOFTWARE'),
    ('Amazon Prime',          14.99, DATEADD('DAY', 15, CURRENT_DATE), 'SHOPPING');